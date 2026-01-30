import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

import Test from './Test';
import AddStore from './AddStore';
import StoreList from './StoreList';

export default function App() {
    return (
        <Router>
            <div className="App">
                {/* Global Navigation Bar */}
                <nav style={{ padding: 10, borderBottom: "1px solid #ccc", display: 'flex', gap: '15px' }}>
                    <Link to="/">Home</Link>
                    <Link to="/my-stores">My Stores</Link>
                    <Link to="/add-store">Add Store</Link>
                </nav>

                {/* The "Switchboard" */}
                <Routes>
                    <Route path="/" element={<Test />} />
                    <Route path="/my-stores" element={<StoreList />} />
                    <Route path="/add-store" element={<AddStore />} />

                    {/* We will add /edit-store/:storeId here later */}
                </Routes>
            </div>
        </Router>
    );
}