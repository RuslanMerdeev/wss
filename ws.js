const ta = document.querySelector('#textarea');

const connect = () => {
  const ws = new WebSocket(`wss://${window.location.host}/ws`);
  ws.addEventListener('message', (evt) => {
    console.log(evt);
    ta.textContent += `${evt.data}\n`;
  });

  ws.addEventListener('open', (evt) => {
    console.log(evt);
    ta.textContent += `connected\n`;
  });

  ws.addEventListener('error', (evt) => {
    console.log(evt);
    ta.textContent += `error\n`;
  });

  ws.addEventListener('close', (evt) => {
    console.log(evt);
    ta.textContent += `closed, try to reconnect\n`;
    setTimeout(connect, 1000);
    ws.close();
  });
};

setTimeout(connect, 1000);
