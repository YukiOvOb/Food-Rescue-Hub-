import React, {useEffect, useState} from 'react'

export default function App(){
  const [msg, setMsg] = useState('');
  const [people, setPeople] = useState([]);

  useEffect(()=>{
    fetch('/api/hello')
      .then(r=>r.json())
      .then(j=>setMsg(j.message))
      .catch(()=>setMsg('Failed to fetch from backend'))

    fetch('/api/people')
      .then(r=>{
        if (!r.ok) throw new Error('network');
        return r.json()
      })
      .then(data=>setPeople(data))
      .catch(()=>setPeople([]))
  },[])

  return (
    <div style={{fontFamily: 'Arial, sans-serif', padding: 24}}>
      <h1>React Hello World</h1>
      <p>Backend says: <strong>{msg}</strong></p>

      <h2>People from test table</h2>
      {people.length === 0 ? (
        <p>No people found (or failed to fetch).</p>
      ) : (
        <table style={{borderCollapse: 'collapse'}}>
          <thead>
            <tr>
              <th style={{border: '1px solid #ccc', padding: 6}}>ID</th>
              <th style={{border: '1px solid #ccc', padding: 6}}>Name</th>
              <th style={{border: '1px solid #ccc', padding: 6}}>Age</th>
              <th style={{border: '1px solid #ccc', padding: 6}}>Gender</th>
            </tr>
          </thead>
          <tbody>
            {people.map(p=> (
              <tr key={p.id}>
                <td style={{border: '1px solid #eee', padding: 6}}>{p.id}</td>
                <td style={{border: '1px solid #eee', padding: 6}}>{p.name}</td>
                <td style={{border: '1px solid #eee', padding: 6}}>{p.age}</td>
                <td style={{border: '1px solid #eee', padding: 6}}>{p.gender}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
