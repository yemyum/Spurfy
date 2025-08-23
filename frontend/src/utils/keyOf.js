// src/utils/keyOf.js
export const keyOf = (m) =>
  m.id || m.serverId || m.clientId ||
  `${m.isUser ? 'U' : 'A'}|${(m.text || '').trim()}|${m.imageUrl || ''}|${String(m.createdAt || m.timestamp).slice(0, 19)}`;