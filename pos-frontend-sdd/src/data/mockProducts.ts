import type { Product } from '../types/product';
export const mockProducts: Product[] = [
{ id:'1', name:'Bananos', category:'Frutas y Verduras', barcode:'100001', price:1.25, taxRate:0, stock:120 },
{ id:'2', name:'Manzanas', category:'Frutas y Verduras', barcode:'100002', price:2.1, taxRate:0, stock:80 },
{ id:'3', name:'Tomates', category:'Frutas y Verduras', barcode:'100003', price:1.9, taxRate:0, stock:60 },
{ id:'4', name:'Leche', category:'Lácteos', barcode:'200001', price:3.5, taxRate:0.05, stock:40 },
{ id:'5', name:'Queso', category:'Lácteos', barcode:'200002', price:4.8, taxRate:0.05, stock:25 },
{ id:'6', name:'Yogur', category:'Lácteos', barcode:'200003', price:1.4, taxRate:0.05, stock:70 },
{ id:'7', name:'Pan', category:'Panadería', barcode:'300001', price:2.4, taxRate:0.04, stock:50 },
{ id:'8', name:'Croissant', category:'Panadería', barcode:'300002', price:1.7, taxRate:0.04, stock:35 },
{ id:'9', name:'Jugo de Naranja', category:'Bebidas', barcode:'400001', price:3.2, taxRate:0.08, stock:45 },
{ id:'10', name:'Botella de Agua', category:'Bebidas', barcode:'400002', price:1.0, taxRate:0.08, stock:200 },
{ id:'11', name:'Gaseosa', category:'Bebidas', barcode:'400003', price:1.6, taxRate:0.08, stock:90 },
{ id:'12', name:'Papas Fritas', category:'Snacks', barcode:'500001', price:2.2, taxRate:0.08, stock:75 },
{ id:'13', name:'Barra de Chocolate', category:'Snacks', barcode:'500002', price:1.3, taxRate:0.08, stock:110 },
{ id:'14', name:'Galletas', category:'Snacks', barcode:'500003', price:2.9, taxRate:0.08, stock:65 },
{ id:'15', name:'Jabón Lavavajillas', category:'Limpieza', barcode:'600001', price:3.1, taxRate:0.12, stock:30 },
{ id:'16', name:'Detergente Ropa', category:'Limpieza', barcode:'600002', price:8.5, taxRate:0.12, stock:20 },
{ id:'17', name:'Toallas de Papel', category:'Limpieza', barcode:'600003', price:5.4, taxRate:0.12, stock:28 },
{ id:'18', name:'Champú', category:'Cuidado Personal', barcode:'700001', price:6.3, taxRate:0.12, stock:32 },
{ id:'19', name:'Pasta Dental', category:'Cuidado Personal', barcode:'700002', price:2.7, taxRate:0.12, stock:55 },
{ id:'20', name:'Jabón de Manos', category:'Cuidado Personal', barcode:'700003', price:2.1, taxRate:0.12, stock:48 }
];
export const categories = ['Todos', ...Array.from(new Set(mockProducts.map(p => p.category)))];
