document.addEventListener('DOMContentLoaded', function () {
    const telefonoInput = document.getElementById('telefono');
    const telefonoErrorDiv = document.getElementById('telefono-error');

    telefonoInput.addEventListener('input', function() {
        const telefonoValue = telefonoInput.value;
        const telefonoRegex = /^\d{9}$/;

        if (telefonoRegex.test(telefonoValue)) {
            telefonoInput.classList.remove('is-invalid');
            telefonoErrorDiv.textContent = '';
        } else {
            telefonoInput.classList.add('is-invalid');
            telefonoErrorDiv.textContent = 'El teléfono debe tener 9 dígitos numéricos.';
        }
    });
});
