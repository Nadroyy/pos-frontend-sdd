type Props={onScan:(barcode:string)=>boolean};
export function CameraScanner({onScan}:Props){const codes=['100001','200001','300001','400001']; return <section className='panel'><h2>Camera Scanner</h2><p className='muted'>Simulated scanner fallback for the workshop.</p><div className='actions'>{codes.map(c=><button key={c} onClick={()=>onScan(c)}>Scan {c}</button>)}</div></section>;}
