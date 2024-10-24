document.addEventListener("DOMContentLoaded", function() {
    const passwordField = document.getElementById("password");
    const passwordConfirmField = document.getElementById("passwordConfirm");
    const passwordRequirements = document.getElementById("password-requirements");
    const form = passwordField.closest("form");

    function validatePassword() {
        const password = passwordField.value;
        const specialCharPattern = /[!@#$%^&*()_+[\]{};':"\\|,.<>?]/g;
        const numberPattern = /\d/;
        const letterPattern = /[a-zA-Z]/;


        if (password.length < 8 || password.length > 16) {
            return "La contraseña debe tener entre 8 y 16 caracteres.";
        }
        if (!numberPattern.test(password)) {
            return "La contraseña debe contener al menos 1 número.";
        }
        if ((password.match(specialCharPattern) || []).length < 2) {
            return "La contraseña debe contener al menos 2 caracteres especiales.";
        }
        if (!letterPattern.test(password)) {
            return "La contraseña debe contener al menos 1 letra.";
        }

        return null;
    }

    passwordField.addEventListener("input", function() {
        const errorMessage = validatePassword();
        if (errorMessage) {
            passwordField.classList.add("is-invalid");
            passwordField.classList.remove("is-valid");
            passwordField.setCustomValidity(errorMessage);
            passwordRequirements.textContent = errorMessage;
        } else {
            passwordField.classList.remove("is-invalid");
            passwordField.classList.add("is-valid");
            passwordField.setCustomValidity("");
            passwordRequirements.textContent = "";
        }
    });

    passwordConfirmField.addEventListener("input", function() {
        if (passwordConfirmField.value !== passwordField.value) {
            passwordConfirmField.classList.add("is-invalid");
            passwordConfirmField.setCustomValidity("Las contraseñas no coinciden.");
            document.getElementById("confirm-message").textContent = "Las contraseñas deben ser iguales.";
        } else {
            passwordConfirmField.classList.remove("is-invalid");
            passwordConfirmField.setCustomValidity("");
            document.getElementById("confirm-message").textContent = "";
        }
    });

    form.addEventListener("submit", function(event) {
        const passwordError = validatePassword();
        if (passwordError) {
            event.preventDefault();
            alert(passwordError);
        }
    });
});
