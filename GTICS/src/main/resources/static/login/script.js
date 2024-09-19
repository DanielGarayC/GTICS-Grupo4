const background = document.querySelector('.background');
        const inputs = document.querySelectorAll('input');
        inputs.forEach(input => {
            input.addEventListener('focus', () => {
                background.style.filter = 'blur(5px)';
            });
            input.addEventListener('blur', () => {
                background.style.filter = 'none';
            });
        });