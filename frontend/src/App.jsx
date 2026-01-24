import React, {useEffect, useState} from 'react'

export default function App(){
  const [msg, setMsg] = useState('');

  useEffect(()=>{
    fetch('/api/hello')
      .then(r=>r.json())
      .then(j=>setMsg(j.message))
      .catch(()=>setMsg('Failed to fetch from backend'))
  },[])

  return (
    <div style={{fontFamily: 'Arial, sans-serif', padding: 24}}>
      <h1>React Hello World</h1>
      <p>Backend says: <strong>{msg}</strong></p>
    </div>
  )
}
