document.addEventListener("DOMContentLoaded", function() {
    const dniInput = document.getElementById("dni");
    const emailInput = document.getElementById("creatFinalUserEmail");

    dniInput.addEventListener('blur', function() {
        verificarYCompletarDNI(dniInput.value);
    });

    emailInput.addEventListener('blur', function() {
        verificarEmail(emailInput.value);
    });
});

function verificarYCompletarDNI(dni) {
    fetch(`/consulta-dni/${dni}`)
        .then(response => response.json())
        .then(data => {
            if (data.length > 0) {
                document.getElementById('Nombre').value = data[0];  // Asumiendo que la posición 0 tiene el nombre
                document.getElementById('ApellidoPaterno').value = data[1];  // Asumiendo que la posición 1 tiene el apellido paterno
                document.getElementById('ApellidoMaterno').value = data[2];  // Asumiendo que la posición 2 tiene el apellido materno
            } else {
                alert("DNI no encontrado");
            }
        })
        .catch(error => console.error('Error:', error));
}

function verificarEmail(email) {
    fetch(`/consulta-email/${email}`)
        .then(response => response.json())
        .then(existe => {
            if (existe) {
                alert('El correo electrónico ya está registrado.');
                document.getElementById('creatFinalUserEmail').value = ''; // Limpiar el campo email
            }
        })
        .catch(error => console.error('Error:', error));
}
