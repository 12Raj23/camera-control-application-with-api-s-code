document.addEventListener("DOMContentLoaded", function () {
  const loginForm = document.getElementById("loginForm");
  const ipPage = document.getElementById("page0");
  const page1 = document.getElementById("page1");
  const page2 = document.getElementById("page2");
  const commandOutput = document.getElementById("commandOutput");
  const startOutput = document.getElementById("startOutput");
  const connectButton = document.getElementById("connectButton");
  const loginOutput = document.getElementById("loginOutput");

  // Initial display: show login form, hide others
  loginForm.style.display = "block";
  ipPage.classList.remove("active");
  page1.classList.remove("active");
  page2.classList.remove("active");

  function isValidIP(ip) {
    const regex = /^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$/;
    return regex.test(ip);
  }

  function isValidPort(port) {
    const num = Number(port);
    return Number.isInteger(num) && num > 0 && num < 65536;
  }

  loginForm.addEventListener("submit", async function (e) {
    e.preventDefault();
    loginOutput.textContent = "";

    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value.trim();

    if (!username || !password) {
      loginOutput.textContent = "Username and password are required.";
      loginOutput.style.color = "red";
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      const text = await res.text();
      loginOutput.textContent = text;
      loginOutput.style.color = res.ok ? "green" : "red";

      if (res.ok) {
        loginForm.style.display = "none";
        ipPage.classList.add("active");
      }
    } catch (err) {
      loginOutput.textContent = "Login failed due to server error.";
      loginOutput.style.color = "red";
      console.error(err);
    }
  });

  const serverForm = document.getElementById("serverForm");
  serverForm.addEventListener("submit", async function (e) {
    e.preventDefault();
    const ipAddress = document.getElementById("ipAddress").value.trim();
    const port = document.getElementById("port").value.trim();

    if (!isValidIP(ipAddress)) {
      startOutput.textContent = "Invalid IP address";
      startOutput.style.color = "red";
      return;
    }
    if (!isValidPort(port)) {
      startOutput.textContent = "Invalid port number";
      startOutput.style.color = "red";
      return;
    }

    connectButton.disabled = true;
    startOutput.textContent = "Connecting...";
    startOutput.style.color = "black";

    try {
      const res = await fetch("http://localhost:8080/connect", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ipAddress, port }),
      });

      if (res.ok) {
        startOutput.textContent = "Connection established!";
        startOutput.style.color = "green";
        ipPage.classList.remove("active");
        page1.classList.add("active");
        commandOutput.textContent = "";
      } else {
        const errorText = await res.text();
        startOutput.textContent = `Connection failed: ${errorText}`;
        startOutput.style.color = "red";
      }
    } catch (err) {
      console.error(err);
      startOutput.textContent = "Error: Could not connect to server";
      startOutput.style.color = "red";
    } finally {
      connectButton.disabled = false;
    }
  });

  async function sendCommand(command) {
    if (commandOutput) {
      commandOutput.textContent = `Command: ${command}`;
    }

    try {
      await fetch("http://localhost:8080/button1", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ value: command }),
      });
    } catch (err) {
      console.error("Error sending command:", err);
    }
  }

  const controlButtons = document.querySelectorAll(".control-button");
  let holdTimeout;
  let isHolding = false;

  controlButtons.forEach((button) => {
    const baseCommand = button.getAttribute("data-value");

    if (baseCommand === "zoom_in" || baseCommand === "zoom_out") {
      const startHold = () => {
        isHolding = false;
        holdTimeout = setTimeout(() => {
          isHolding = true;
          sendCommand(baseCommand + "_long");
        }, 500);
      };

      const endHold = () => {
        clearTimeout(holdTimeout);
        if (!isHolding) {
          sendCommand(baseCommand);
        }
      };

      button.addEventListener("mousedown", startHold);
      button.addEventListener("mouseup", endHold);
      button.addEventListener("mouseleave", () => clearTimeout(holdTimeout));
      button.addEventListener("touchstart", (e) => {
        e.preventDefault();
        startHold();
      }, { passive: false });
      button.addEventListener("touchend", (e) => {
        e.preventDefault();
        endHold();
      }, { passive: false });
    } else {
      button.addEventListener("click", () => sendCommand(baseCommand));
      button.addEventListener("touchstart", (e) => {
        e.preventDefault();
        sendCommand(baseCommand);
      }, { passive: false });
    }
  });

  document.getElementById("nextPage")?.addEventListener("click", function () {
    page1.classList.remove("active");
    page2.classList.add("active");
    commandOutput.textContent = "Other Controls Page";
  });

  document.getElementById("prevPage")?.addEventListener("click", function () {
    page2.classList.remove("active");
    page1.classList.add("active");
    commandOutput.textContent = "";
  });
});
