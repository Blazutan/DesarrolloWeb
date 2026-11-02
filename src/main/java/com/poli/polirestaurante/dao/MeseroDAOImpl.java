package com.poli.polirestaurante.dao;

import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.repository.MeseroRepository;
import com.poli.polirestaurante.security.PasswordEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeseroDAOImpl implements MeseroDAO {

    private final MeseroRepository meseroRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MeseroDAOImpl(MeseroRepository meseroRepository, PasswordEncoder passwordEncoder) {
        this.meseroRepository = meseroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mesero crear(Mesero objeto) {
        codificarPassword(objeto);
        return meseroRepository.save(objeto);
    }

    @Override
    public List<Mesero> listar() {
        return meseroRepository.findAll();
    }

    @Override
    public Mesero buscarPorId(Long id) {
        return meseroRepository.findById(id).orElse(null);
    }

    @Override
    public Mesero buscarPorCorreo(String correo) {
        return meseroRepository.findByCorreo(correo).orElse(null);
    }

    @Override
    public Mesero actualizar(Mesero objeto) {
        codificarPassword(objeto);
        return meseroRepository.save(objeto);
    }

    private void codificarPassword(Mesero mesero) {
        mesero.setPassword(PasswordEncoding.encodeIfNeeded(mesero.getPassword(), passwordEncoder));
    }

    @Override
    public boolean eliminar(Long id) {
        if (!meseroRepository.existsById(id)) {
            return false;
        }
        meseroRepository.deleteById(id);
        return true;
    }
}