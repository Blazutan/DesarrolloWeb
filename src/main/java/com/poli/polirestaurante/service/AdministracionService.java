package com.poli.polirestaurante.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.poli.polirestaurante.dao.AdministradorDAO;
import com.poli.polirestaurante.dao.CocinaDAO;
import com.poli.polirestaurante.dao.MesaDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dto.MesaResponse;
import com.poli.polirestaurante.dto.MesaRequest;
import com.poli.polirestaurante.dto.UsuarioRequest;
import com.poli.polirestaurante.dto.UsuarioResponse;
import com.poli.polirestaurante.modelo.Administrador;
import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.modelo.Mesero;

@Service
public class AdministracionService {

    private static final String MESERO = "MESERO";
    private static final String COCINA = "COCINA";
    private static final String ADMINISTRADOR = "ADMINISTRADOR";

    private final MesaDAO mesaDAO;
    private final MeseroDAO meseroDAO;
    private final CocinaDAO cocinaDAO;
    private final AdministradorDAO administradorDAO;

    public AdministracionService(MesaDAO mesaDAO, MeseroDAO meseroDAO, CocinaDAO cocinaDAO,
            AdministradorDAO administradorDAO) {
        this.mesaDAO = mesaDAO;
        this.meseroDAO = meseroDAO;
        this.cocinaDAO = cocinaDAO;
        this.administradorDAO = administradorDAO;
    }

    public List<MesaResponse> listarMesas() {
        return mesaDAO.listar().stream().map(this::mesaResponse).toList();
    }

    public MesaResponse buscarMesa(Long id) {
        Mesa mesa = mesaDAO.buscarPorId(id);
        return mesa == null ? null : mesaResponse(mesa);
    }

    public MesaResponse crearMesa(MesaRequest datos) {
        validarMesa(datos);
        return mesaResponse(mesaDAO.crear(new Mesa(datos.numero(), normalizarEstadoMesa(datos.estado()))));
    }

    public MesaResponse actualizarMesa(Long id, MesaRequest datos) {
        validarMesa(datos);
        Mesa mesa = mesaDAO.buscarPorId(id);
        if (mesa == null) {
            return null;
        }
        mesa.setNumero(datos.numero());
        mesa.setEstado(normalizarEstadoMesa(datos.estado()));
        return mesaResponse(mesaDAO.actualizar(mesa));
    }

    public boolean eliminarMesa(Long id) {
        return mesaDAO.eliminar(id);
    }

    public MesaResponse actualizarEstadoMesa(Long id, String estado) {
        Mesa mesa = mesaDAO.buscarPorId(id);
        if (mesa == null) {
            return null;
        }
        mesa.setEstado(normalizarEstadoMesa(estado));
        return mesaResponse(mesaDAO.actualizar(mesa));
    }

    public List<UsuarioResponse> listarUsuarios() {
        List<UsuarioResponse> usuarios = new ArrayList<>();
        meseroDAO.listar().forEach(mesero -> usuarios.add(usuarioResponse(mesero, MESERO)));
        cocinaDAO.listar().forEach(cocina -> usuarios.add(usuarioResponse(cocina, COCINA)));
        administradorDAO.listar().forEach(admin -> usuarios.add(usuarioResponse(admin, ADMINISTRADOR)));
        return usuarios;
    }

    public UsuarioResponse buscarUsuario(String rol, Long id) {
        return switch (normalizarRol(rol)) {
            case MESERO -> {
                Mesero mesero = meseroDAO.buscarPorId(id);
                yield mesero == null ? null : usuarioResponse(mesero, MESERO);
            }
            case COCINA -> {
                Cocina cocina = cocinaDAO.buscarPorId(id);
                yield cocina == null ? null : usuarioResponse(cocina, COCINA);
            }
            case ADMINISTRADOR -> {
                Administrador administrador = administradorDAO.buscarPorId(id);
                yield administrador == null ? null : usuarioResponse(administrador, ADMINISTRADOR);
            }
            default -> throw new IllegalArgumentException("Rol de usuario no válido.");
        };
    }

    public UsuarioResponse crearUsuario(String rol, UsuarioRequest datos) {
        String rolNormalizado = normalizarRol(rol);
        validarDatos(datos, true);
        String correo = normalizarCorreo(datos.correo());
        validarCorreoUnico(correo, null, null);

        return switch (rolNormalizado) {
            case MESERO -> usuarioResponse(meseroDAO.crear(new Mesero(datos.nombre().trim(), correo, datos.password())), MESERO);
            case COCINA -> usuarioResponse(cocinaDAO.crear(new Cocina(datos.nombre().trim(), correo, datos.password())), COCINA);
            case ADMINISTRADOR -> usuarioResponse(
                    administradorDAO.crear(new Administrador(datos.nombre().trim(), correo, datos.password())),
                    ADMINISTRADOR);
            default -> throw new IllegalArgumentException("Rol de usuario no válido.");
        };
    }

    public UsuarioResponse actualizarUsuario(String rol, Long id, UsuarioRequest datos) {
        String rolNormalizado = normalizarRol(rol);
        validarDatos(datos, false);
        String correo = normalizarCorreo(datos.correo());
        validarCorreoUnico(correo, rolNormalizado, id);

        return switch (rolNormalizado) {
            case MESERO -> {
                Mesero mesero = meseroDAO.buscarPorId(id);
                if (mesero == null) {
                    yield null;
                }
                mesero.setNombre(datos.nombre().trim());
                mesero.setCorreo(correo);
                if (datos.password() != null && !datos.password().isBlank()) {
                    mesero.setPassword(datos.password());
                }
                yield usuarioResponse(meseroDAO.actualizar(mesero), MESERO);
            }
            case COCINA -> {
                Cocina cocina = cocinaDAO.buscarPorId(id);
                if (cocina == null) {
                    yield null;
                }
                cocina.setNombre(datos.nombre().trim());
                cocina.setCorreo(correo);
                if (datos.password() != null && !datos.password().isBlank()) {
                    cocina.setPassword(datos.password());
                }
                yield usuarioResponse(cocinaDAO.actualizar(cocina), COCINA);
            }
            case ADMINISTRADOR -> {
                Administrador admin = administradorDAO.buscarPorId(id);
                if (admin == null) {
                    yield null;
                }
                admin.setNombre(datos.nombre().trim());
                admin.setCorreo(correo);
                if (datos.password() != null && !datos.password().isBlank()) {
                    admin.setPassword(datos.password());
                }
                yield usuarioResponse(administradorDAO.actualizar(admin), ADMINISTRADOR);
            }
            default -> throw new IllegalArgumentException("Rol de usuario no válido.");
        };
    }

    public boolean eliminarUsuario(String rol, Long id) {
        return switch (normalizarRol(rol)) {
            case MESERO -> meseroDAO.eliminar(id);
            case COCINA -> cocinaDAO.eliminar(id);
            case ADMINISTRADOR -> administradorDAO.eliminar(id);
            default -> throw new IllegalArgumentException("Rol de usuario no válido.");
        };
    }

    private void validarDatos(UsuarioRequest datos, boolean passwordRequired) {
        if (datos == null || datos.nombre() == null || datos.nombre().isBlank()
                || datos.correo() == null || datos.correo().isBlank()) {
            throw new IllegalArgumentException("Nombre y correo son obligatorios.");
        }
        if (passwordRequired && (datos.password() == null || datos.password().isBlank())) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear una cuenta.");
        }
    }

    private void validarMesa(MesaRequest datos) {
        if (datos == null || datos.numero() < 1) {
            throw new IllegalArgumentException("El número de mesa debe ser mayor que cero.");
        }
        normalizarEstadoMesa(datos.estado());
    }

    private String normalizarEstadoMesa(String estado) {
        String estadoNormalizado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        if (!"LIBRE".equals(estadoNormalizado) && !"OCUPADA".equals(estadoNormalizado)) {
            throw new IllegalArgumentException("El estado de una mesa debe ser LIBRE u OCUPADA.");
        }
        return estadoNormalizado;
    }

    private void validarCorreoUnico(String correo, String rolActual, Long idActual) {
        boolean duplicadoMesero = meseroDAO.listar().stream()
                .anyMatch(usuario -> correo.equalsIgnoreCase(usuario.getCorreo())
                        && !(MESERO.equals(rolActual) && usuario.getId().equals(idActual)));
        boolean duplicadoCocina = cocinaDAO.listar().stream()
                .anyMatch(usuario -> correo.equalsIgnoreCase(usuario.getCorreo())
                        && !(COCINA.equals(rolActual) && usuario.getId().equals(idActual)));
        boolean duplicadoAdmin = administradorDAO.listar().stream()
                .anyMatch(usuario -> correo.equalsIgnoreCase(usuario.getCorreo())
                        && !(ADMINISTRADOR.equals(rolActual) && usuario.getId().equals(idActual)));
        if (duplicadoMesero || duplicadoCocina || duplicadoAdmin) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
    }

    private String normalizarRol(String rol) {
        String normalizado = rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
        if (!MESERO.equals(normalizado) && !COCINA.equals(normalizado) && !ADMINISTRADOR.equals(normalizado)) {
            throw new IllegalArgumentException("Rol de usuario no válido.");
        }
        return normalizado;
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private MesaResponse mesaResponse(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumero(), mesa.getEstado());
    }

    private UsuarioResponse usuarioResponse(Mesero mesero, String rol) {
        return new UsuarioResponse(mesero.getId(), mesero.getNombre(), mesero.getCorreo(), rol);
    }

    private UsuarioResponse usuarioResponse(Cocina cocina, String rol) {
        return new UsuarioResponse(cocina.getId(), cocina.getNombre(), cocina.getCorreo(), rol);
    }

    private UsuarioResponse usuarioResponse(Administrador administrador, String rol) {
        return new UsuarioResponse(administrador.getId(), administrador.getNombre(), administrador.getCorreo(), rol);
    }
}