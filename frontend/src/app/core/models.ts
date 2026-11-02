export interface Categoria {
  id: number;
  nombre: string;
}

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  disponibilidad: boolean;
  categoriaId: number | null;
  categoriaNombre: string | null;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  precio: number;
  disponibilidad: boolean;
  categoriaId: number;
}

export interface Mesa {
  id: number;
  numero: number;
  estado: 'LIBRE' | 'OCUPADA';
}

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: 'MESERO' | 'COCINA' | 'ADMINISTRADOR';
}

export interface UsuarioRequest {
  nombre: string;
  correo: string;
  password?: string;
}

export interface Pedido {
  id: number;
  meseroId: number;
  estado: string;
  fechaCreacion: string;
  fechaUltimaActualizacion: string;
  fechaEnPreparacion: string | null;
  fechaListo: string | null;
  fechaEntregado: string | null;
  precioTotal: number;
  numeroRecordatorios: number;
  alertaRecibida: boolean;
  motivoCancelacion: string | null;
  mesaId: number | null;
  mesaNumero: number | null;
}

export interface PedidoItemRequest {
  productoId: number;
  cantidad: number;
  notas: string;
}

export interface PedidoRequest {
  mesaId: number;
  detalles: PedidoItemRequest[];
}

export interface DetallePedido {
  id: number;
  pedidoId: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  notas: string | null;
  precioUnitario: number;
  subtotal: number;
}

export interface Alerta {
  pedidoId: number;
  fechaListo: string;
  numeroRecordatorios: number;
}

export interface ReporteVentas {
  desde: string;
  hasta: string;
  totalPedidos: number;
  totalVentas: number;
  ticketPromedio: number;
  productos: Array<{ producto: string; unidades: number; total: number }>;
}

export interface ReporteTiempos {
  desde: string;
  hasta: string;
  pedidosAnalizados: number;
  promedioPreparacionSegundos: number | null;
  promedioEsperaEntregaSegundos: number | null;
  promedioTotalSegundos: number | null;
}
