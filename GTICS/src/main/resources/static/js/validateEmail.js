document.addEventListener("DOMContentLoaded", function () {
    const emailField = document.getElementById("email");
    const emailError = document.getElementById("email-error");
    const emailIcon = document.createElement("span");

    emailIcon.classList.add("icon-error");
    emailField.parentElement.appendChild(emailIcon);

    function validateEmail() {
        const email = emailField.value.trim();
        if (email === "") {
            return "El correo no puede estar vacío.";
        }
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailPattern.test(email)) {
            return "El formato del correo es inválido.";
        }
        return null;
    }

    emailField.addEventListener("input", function () {
        const errorMessage = validateEmail();
        if (errorMessage) {
            emailField.classList.add("is-invalid");
            emailField.classList.remove("is-valid");
            emailError.textContent = errorMessage;
            emailIcon.style.display = "block";
        } else {
            fetch(`consulta-email/${emailField.value}`)
                .then(response => response.json())
                .then(data => {
                    if (data) {
                        emailField.classList.add("is-invalid");
                        emailField.classList.remove("is-valid");
                        emailError.textContent = "Este correo ya está registrado.";
                        emailIcon.style.display = "block"; // Mostrar el icono
                    } else {
                        emailField.classList.remove("is-invalid");
                        emailField.classList.add("is-valid");
                        emailError.textContent = "";
                        emailIcon.style.display = "none"; // Ocultar el icono
                    }
                })
                .catch(error => {
                    console.error("Error al validar el correo:", error);
                    emailField.classList.add("is-invalid");
                    emailError.textContent = "Error al validar el correo.";
                    emailIcon.style.display = "block";
                });
        }
    });

    emailField.addEventListener("blur", function () {
        const errorMessage = validateEmail();
        if (errorMessage) {
            emailField.classList.add("is-invalid");
            emailField.classList.remove("is-valid");
            emailError.textContent = errorMessage;
            emailIcon.style.display = "block";
        }
    });
});
