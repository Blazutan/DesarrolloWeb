export interface Categoria { id: number; nombre: string; }
export interface Producto { id: number; nombre: string; descripcion: string | null; precio: number; disponibilidad: boolean; categoriaId: number | null; categoriaNombre: string | null; }
export interface Pedido { id: number; estado: string; precioTotal: number; fechaCreacion: string; }
export interface Alerta { pedidoId: number; fechaListo: string; numeroRecordatorios: number; }
