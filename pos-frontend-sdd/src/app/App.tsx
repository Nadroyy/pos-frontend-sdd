import { useMemo, useState } from 'react';
import { ProductSearch } from '../components/product/ProductSearch';
import { BarcodeInput } from '../components/product/BarcodeInput';
import { CameraScanner } from '../components/product/CameraScanner';
import { CartPanel } from '../components/cart/CartPanel';
import { CheckoutPanel } from '../components/checkout/CheckoutPanel';
import { ReceiptView } from '../components/receipt/ReceiptView';
import { categories } from '../data/mockProducts';
import { searchProducts, findProductByBarcode } from '../services/productService';
import { useCart } from '../hooks/useCart';
import { useOnlineStatus } from '../hooks/useOnlineStatus';
import type { Receipt } from '../types/receipt';
export function App(){const online=useOnlineStatus(); const cart=useCart(); const [searchTerm,setSearchTerm]=useState(''); const [category,setCategory]=useState('All'); const [receipt,setReceipt]=useState<Receipt|null>(null); const products=useMemo(()=>searchProducts(searchTerm,category),[searchTerm,category]); function scanBarcode(barcode:string):boolean{const product=findProductByBarcode(barcode); if(!product)return false; cart.addProduct(product); return true;} function newSale(){setReceipt(null); cart.clearCart();} return <main><header className='topbar'><div><h1>Supermarket POS</h1><p>Spec-Driven Development Workshop</p></div><span className={online?'status online':'status offline'}>{online?'Online':'Offline'}</span></header>{receipt?<ReceiptView receipt={receipt} onNewSale={newSale}/>:<div className='layout'><div className='left'><ProductSearch products={products} searchTerm={searchTerm} category={category} categories={categories} onSearchChange={setSearchTerm} onCategoryChange={setCategory} onAdd={cart.addProduct}/><div className='grid two'><BarcodeInput onScan={scanBarcode}/><CameraScanner onScan={scanBarcode}/></div></div><div className='right-col'><CartPanel items={cart.items} summary={cart.summary} discount={cart.discount} onQuantity={cart.updateQuantity} onRemove={cart.removeProduct} onDiscount={cart.setDiscount}/><CheckoutPanel items={cart.items} summary={cart.summary} onComplete={setReceipt}/></div></div>}</main>;}
