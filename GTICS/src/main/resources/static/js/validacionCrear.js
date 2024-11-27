document.addEventListener("DOMContentLoaded", function() {
    const dniInput = document.getElementById('dni');
    const nombreInput = document.getElementById('Nombre');
    const apellidoPaternoInput = document.getElementById('ApellidoPaterno');
    const apellidoMaternoInput = document.getElementById('ApellidoMaterno');
    const emailInput = document.getElementById('email');
    const crearButton = document.getElementById('crearButton');

    const dniError = document.getElementById('dni-error');
    const emailError = document.getElementById('email-error');

    dniInput.addEventListener('input', function() {
        const dni = dniInput.value;
        if (dni.length === 8 && /^\d+$/.test(dni)) {
            verificarDniExistente(dni);
        } else {
            resetForm();
            dniError.textContent = 'El DNI debe contener exactamente 8 dígitos numéricos.';
            dniInput.classList.add('is-invalid');
            crearButton.disabled = true;
        }
    });

    function verificarDniExistente(dni) {
        fetch(`/existe-dni/${dni}`)
            .then(response => response.json())
            .then(existe => {
                if (existe) {
                    dniError.textContent = 'El DNI ingresado ya está registrado en el sistema.';
                    dniInput.classList.add('is-invalid');
                    resetForm();
                    crearButton.disabled = true;
                } else {
                    dniError.textContent = '';
                    dniInput.classList.remove('is-invalid');
                    consultarDNI(dni);
                }
            })
            .catch(error => {
                console.error('Error al verificar el DNI:', error);
                crearButton.disabled = true;
            });
    }

    function consultarDNI(dni) {
        fetch(`/consulta-dni/${dni}`)
            .then(response => response.json())
            .then(datos => {
                if (datos.length > 0) {
                    nombreInput.value = datos[0];
                    apellidoPaternoInput.value = datos[1];
                    apellidoMaternoInput.value = datos[2];
                    nombreInput.disabled = true;
                    apellidoPaternoInput.disabled = true;
                    apellidoMaternoInput.disabled = true;
                    crearButton.disabled = false;
                    dniInput.classList.add('is-valid');
                    dniInput.classList.remove('is-invalid');
                } else {
                    resetForm();
                    dniError.textContent = 'DNI no encontrado.';
                    dniInput.classList.remove('is-valid');
                    dniInput.classList.add('is-invalid');
                    crearButton.disabled = true;
                }
            })
            .catch(error => {
                console.error('Error al consultar el DNI:', error);
                crearButton.disabled = true;
            });
    }

    function resetForm() {
        nombreInput.value = '';
        apellidoPaternoInput.value = '';
        apellidoMaternoInput.value = '';
        nombreInput.disabled = false;
        apellidoPaternoInput.disabled = false;
        apellidoMaternoInput.disabled = false;
    }

    emailInput.addEventListener('input', function() {
        const email = emailInput.value;
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (emailPattern.test(email)) {
            emailInput.classList.add('is-valid');
            emailInput.classList.remove('is-invalid');
            verificarEmailExistente(email);
        } else {
            emailError.textContent = 'Por favor ingrese un correo electrónico válido.';
            emailInput.classList.remove('is-valid');
            emailInput.classList.add('is-invalid');
            crearButton.disabled = true;
        }
    });

    function verificarEmailExistente(email) {
        fetch(`/consulta-email/${email}`)
            .then(response => response.json())
            .then(existe => {
                if (existe) {
                    emailError.textContent = 'El correo electrónico ya está registrado en el sistema.';
                    emailInput.classList.remove('is-valid');
                    emailInput.classList.add('is-invalid');
                    crearButton.disabled = true;
                } else {
                    emailError.textContent = '';
                    emailInput.classList.add('is-valid');
                    emailInput.classList.remove('is-invalid');
                    crearButton.disabled = false;
                }
            })
            .catch(error => {
                console.error('Error al verificar el correo electrónico:', error);
                crearButton.disabled = true;
            });
    }

    function validarFormulario() {
        const inputsRequeridos = document.querySelectorAll('.form-control');
        return Array.from(inputsRequeridos).every(input => input.classList.contains('is-valid'));
    }
});
