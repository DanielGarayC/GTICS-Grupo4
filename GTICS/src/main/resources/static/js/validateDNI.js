document.getElementById("dni").addEventListener("blur", function () {
    const dni = this.value.trim();
    const dniError = document.getElementById("dni-error");

    dniError.style.display = "none";
    dniError.textContent = "";

    if (dni.length === 8 && /^[0-9]+$/.test(dni)) {
        fetch(`consulta-dni/${dni}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("DNI no encontrado o error en la API.");
                }
                return response.json();
            })
            .then(data => {
                document.getElementById("Nombre").value = data[0];
                document.getElementById("ApellidoPaterno").value = data[1];
                document.getElementById("ApellidoMaterno").value = data[2];

                document.getElementById("Nombre").readOnly = true;
                document.getElementById("ApellidoPaterno").readOnly = true;
                document.getElementById("ApellidoMaterno").readOnly = true;
            })
            .catch(error => {
                dniError.style.display = "block";
                dniError.textContent = error.message;

                document.getElementById("Nombre").value = "";
                document.getElementById("ApellidoPaterno").value = "";
                document.getElementById("ApellidoMaterno").value = "";

                document.getElementById("Nombre").readOnly = false;
                document.getElementById("ApellidoPaterno").readOnly = false;
                document.getElementById("ApellidoMaterno").readOnly = false;
            });
    } else {

        dniError.style.display = "block";
        dniError.textContent = "Por favor, ingresa un DNI válido de 8 dígitos.";

        document.getElementById("Nombre").value = "";
        document.getElementById("ApellidoPaterno").value = "";
        document.getElementById("ApellidoMaterno").value = "";

        document.getElementById("Nombre").readOnly = false;
        document.getElementById("ApellidoPaterno").readOnly = false;
        document.getElementById("ApellidoMaterno").readOnly = false;
    }
});
