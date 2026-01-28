import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

// 1. Import your group's pages
import Test from './Test';   // The code you just moved
import AddStore from './AddStore'; // The new feature we just built
// import Inventory from './pages/Inventory'; (Example of a groupmate's file)
// import Login from './pages/Login';         (Example of another file)

export default function App() {
    return (
        <Router>
            <div className="App">
                {/* Global Navigation Bar (Always visible) */}
                <nav style={{ padding: 10, borderBottom: "1px solid #ccc" }}>
                    <Link to="/" style={{ marginRight: 10 }}>Home</Link>
                    <Link to="/add-store" style={{ marginRight: 10 }}>Add Store</Link>
                    {/* <Link to="/inventory">Inventory</Link> */}
                </nav>

                {/* The "Switchboard" - Only ONE of these shows at a time */}
                <Routes>
                    <Route path="/" element={<Test />} />
                    <Route path="/add-store" element={<AddStore />} />

                    {/* Future Group Work */}
                    {/* <Route path="/inventory" element={<Inventory />} /> */}
                    {/* <Route path="/login" element={<Login />} /> */}
                </Routes>
            </div>
        </Router>
    );
}