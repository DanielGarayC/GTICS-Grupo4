document.querySelectorAll('.eliminar-producto').forEach(function (btn) {
    btn.addEventListener('click', function () {
        const productoId = this.getAttribute('data-producto-id');  // Obtener el ID del producto correctamente
        console.log('Producto ID:', productoId);  // Verificar si el productoId se está obteniendo correctamente

        if (!productoId) {
            alert('No se encontró el ID del producto.');
            return;
        }

        // Realizar la solicitud DELETE con el ID del producto
        fetch(`/UsuarioFinal/eliminarProductoCarrito/${productoId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al eliminar el producto.');  // Si la respuesta no es exitosa
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    // Eliminar el producto del carrito en el frontend
                    document.querySelector(`.producto-item-${productoId}`).remove();
                } else {
                    alert('Error al eliminar el producto: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ocurrió un error al eliminar el producto.');
            });
    });
});
