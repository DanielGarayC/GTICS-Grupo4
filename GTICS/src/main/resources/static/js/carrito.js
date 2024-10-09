// Delegación de eventos para evitar duplicados
document.addEventListener("DOMContentLoaded", function () {
    const carritoContainer = document.querySelector(".offcanvas-body");

    // Delegar el evento para los botones de incrementar y decrementar
    carritoContainer.addEventListener("click", function (event) {
        const target = event.target;

        if (target.classList.contains("button-plus")) {
            const input = target.closest(".input-spinner").querySelector(".quantity-field");
            let currentValue = parseInt(input.value, 10);
            if (!isNaN(currentValue)) {
                input.value = currentValue + 1;
                actualizarCantidad(input.dataset.productoId, input.value);
            }
        }

        if (target.classList.contains("button-minus")) {
            const input = target.closest(".input-spinner").querySelector(".quantity-field");
            let currentValue = parseInt(input.value, 10);
            if (!isNaN(currentValue) && currentValue > 1) {
                input.value = currentValue - 1;
                actualizarCantidad(input.dataset.productoId, input.value);
            }
        }

        // Delegar el evento para el botón de eliminar producto
        if (target.closest(".eliminar-producto")) {
            const button = target.closest(".eliminar-producto");
            const idProducto = button.dataset.productoId;
            eliminarProducto(idProducto);
        }
    });
});

// Función para actualizar la cantidad del producto en el backend
function actualizarCantidad(idProducto, cantidad) {
    fetch(`/UsuarioFinal/actualizarCantidad`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ idProducto, cantidad }),
    })
        .then(response => response.json())
        .then(data => {
            console.log('Cantidad actualizada:', data);
            // Puedes actualizar el precio total o realizar otras acciones aquí
        })
        .catch(error => {
            console.error('Error al actualizar la cantidad:', error);
        });
}

// Función para eliminar el producto
function eliminarProducto(idProducto) {
    fetch(`/UsuarioFinal/eliminarProductoDelCarrito`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `idProducto=${idProducto}`,
    })
        .then(response => {
            if (response.ok) {
                // Eliminar el producto del DOM
                const item = document.querySelector(`.producto-item-${idProducto}`);
                if (item) {
                    item.remove();
                }
                console.log(`Producto ${idProducto} eliminado del carrito.`);
            } else {
                console.error('Error al eliminar el producto del carrito');
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

document.querySelectorAll(".eliminar-producto").forEach(button => {
    button.addEventListener("click", function (event) {
        event.preventDefault(); // Prevent any default action
        const idProducto = button.dataset.productoId;
        eliminarProducto(idProducto);
    });
});