type Props={onScan:(barcode:string)=>boolean};
export function CameraScanner({onScan}:Props){const codes=['100001','200001','300001','400001']; return <section className='panel'><h2>Escáner de Cámara</h2><p className='muted'>Escáner simulado para el taller.</p><div className='actions'>{codes.map(c=><button key={c} onClick={()=>onScan(c)}>Escanear {c}</button>)}</div></section>;}
