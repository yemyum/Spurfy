// App.jsx
import React from 'react';
import './App.css';
import './index.css';
import routeList from './routes.jsx';
import { Routes, Route } from 'react-router-dom';

function App() {
  return (
    <Routes>
      {routeList.map(({ path, element, children }, idx) => (
        <Route key={idx} path={path} element={element}>
          {children &&
            children.map((child, cIdx) => (
              <Route key={cIdx} path={child.path} element={child.element} />
            ))}
        </Route>
      ))}
    </Routes>
  );
}

export default App;