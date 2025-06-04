import React from 'react';
import './App.css';
import './index.css';
import routeList from './routes.jsx';
import { Routes, Route } from 'react-router-dom';

function App() {
  return (
    <>
      <Routes>
        {routeList.map(({ path, element }, idx) => (
          <Route key={idx} path={path} element={element} />
        ))}
      </Routes>
    </>
  );
}

export default App;
