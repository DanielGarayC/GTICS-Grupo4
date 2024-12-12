// Función para animar el icono del carrito
function animateCartIcon() {
    // Seleccionar los elementos SVG del carrito (desktop y mobile)
    const cartIconDesktop = document.querySelector('#carrito-count-desktop').previousElementSibling;
    const cartIconMobile = document.querySelector('#carrito-count-mobile').previousElementSibling;

    // Lista de iconos a animar
    const cartIcons = [cartIconDesktop, cartIconMobile];

    cartIcons.forEach(icon => {
        if (icon) {
            // Remover clases de animación si ya están presentes
            icon.classList.remove('animate-bounce', 'animate-scale');

            // Forzar reflow para reiniciar la animación
            void icon.offsetWidth;

            // Agregar la clase de animación deseada
            icon.classList.add('animate-bounce'); // Puedes cambiar a 'animate-scale' si prefieres otra animación

            // Remover la clase de animación después de que termine
            icon.addEventListener('animationend', () => {
                icon.classList.remove('animate-bounce', 'animate-scale');
            }, { once: true });
        }
    });
}

// Función para actualizar los contadores del carrito
function updateCartCounters(totalArticulos) {
    $('#carrito-count-desktop').text(totalArticulos);
    $('#carrito-count-mobile').text(totalArticulos);
    animateCartIcon();
}

$(document).ready(function () {
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajaxSetup({
        beforeSend: function(xhr) {
            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });

    // Manejo del formulario de agregar al carrito (UNICO .on('submit'))
    $('.add-to-cart-form').on('submit', function(event) {
        event.preventDefault();
        var form = $(this);
        var context = form.data('context'); // 'modal', 'general', 'recomendados', etc.
        var button = form.find('button[type="submit"]');
        var originalButtonText = button.html(); // Guardamos el texto original

        // Verificación de stock si aplica
        var cantidadVisibleInput = form.find('input[name="cantidadVisible"], input[name="cantidad"]');
        var cantidad = parseInt(cantidadVisibleInput.val());
        var maxStock = parseInt(cantidadVisibleInput.attr('max')) || 9999;

        if (cantidad > maxStock) {
            alert('La cantidad seleccionada excede el stock disponible.');
            return;
        }

        // Verificar que la cantidad sea válida
        if (isNaN(cantidad) || cantidad < 1) {
            alert('Cantidad inválida.');
            return;
        }

        button.prop('disabled', true);
        button.addClass('btn-adding');
        // Cambiar el texto temporalmente
        button.html('Agregando...');
        $.ajax({
            url: form.attr('action'),
            type: form.attr('method'),
            data: form.serialize(),
            dataType: 'json',
            success: function(response) {
                if (response.success) {
                    // Actualizar contadores del carrito
                    updateCartCounters(response.totalArticulos);

                    // Actualizar el contenido del carrito si está abierto
                    if ($('#offcanvasRight').hasClass('show')) {
                        fetchCartItems();
                    }

                    // Manejo según el contexto
                    if (context === 'modal') {
                        // Cerrar el modal si es contexto modal
                        var modal = form.closest('.modal');
                        if (modal.length) {
                            modal.modal('hide');
                        }
                    } else if (context === 'recomendados') {
                        console.log('Agregado desde recomendados');
                    } else if (context === 'general') {
                        console.log('Agregado desde vista general');
                    }

                    // Efecto de éxito:
                    button.removeClass('btn-adding');
                    button.html('&nbsp;');
                    button.addClass('btn-added-success');

                    // Después de 1 segundo, volver al estado original
                    setTimeout(function(){
                        button.removeClass('btn-added-success');
                        button.html(originalButtonText);
                    }, 1290);
                } else {
                    // Si hubo error, restaurar el botón al estado original
                    button.removeClass('btn-adding');
                    button.html(originalButtonText);
                }
            },
            error: function(xhr, status, error) {
                console.error('Error AJAX:', error);
                // Error, restaurar el botón
                button.removeClass('btn-adding');
                button.html(originalButtonText);
            },
            complete: function() {
                // Re-habilitar siempre el botón al finalizar
                button.prop('disabled', false);
            }
        });
    });

    // Función para obtener y renderizar los productos del carrito
    function fetchCartItems() {
        $.ajax({
            url: '/UsuarioFinal/listaCarrito',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                var cartList = $('#cart-items');
                cartList.empty(); // Limpiar la lista actual

                if (data.length > 0) {
                    data.forEach(function (producto) {
                        var listItem = `
                            <li class="list-group-item py-3 ps-0 border-top producto-item-${producto.idProducto}">
                                <div class="row align-items-center">
                                    <div class="col-7 col-md-7 col-lg-7">
                                        <div class="d-flex">
                                            <img src="${producto.urlImagenProducto}" alt="Producto" class="icon-shape icon-xxl" />
                                            <div class="ms-3">
                                                <a href="/UsuarioFinal/detallesProducto/${producto.idProducto}" class="text-inherit">
                                                    <h6 class="mb-0">${producto.nombreProducto}</h6>
                                                </a>
                                                <div class="mt-2 mb-2">
                                                    <span class="fw-bold" style="font-size: 0.9em;">S/. ${producto.precioTotalPorProducto.toFixed(2)}</span>
                                                </div>
                                                <div class="mt-2 small lh-1">
                                                    <button type="button" class="text-decoration-none text-inherit eliminar-producto btn btn-link" data-id="${producto.idProducto}" style="padding: 0; border: none; background: none;">
                                                        <span class="me-1 align-text-bottom">
                                                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-trash-2 text-success">
                                                                <polyline points="3 6 5 6 21 6"></polyline>
                                                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                                <line x1="10" y1="11" x2="10" y2="17"></line>
                                                                <line x1="14" y1="11" x2="14" y2="17"></line>
                                                            </svg>
                                                        </span>
                                                        <span class="text-muted">Eliminar</span>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-5 col-md-5 col-lg-5">
                                        <div class="d-flex align-items-center">
                                            <div class="input-group input-spinner" style="max-width: 150px; display: flex; align-items: center;">
                                                <input type="button" value="-" class="button-minus btn btn-xs btn-outline-secondary" onclick="this.parentNode.querySelector('input[type=number]').stepDown()" style="width: 35px; padding: 0;"/>
                                                <input type="number" name="nuevaCantidad" value="${producto.cantidadProducto}" step="1"  min="1" max="${producto.cantidadDisponible}" class="quantity-field form-control text-center" style="max-width: 50px; height: 35px; padding: 0;" />
                                                <input type="button" value="+" class="button-plus btn btn-xs btn-outline-secondary" onclick="this.parentNode.querySelector('input[type=number]').stepUp()" style="width: 35px; padding: 0;"/>
                                            </div>
                                            <button type="button" class="btn btn-primary btn-sm ms-2 actualizar-cantidad" data-id="${producto.idProducto}">Actualizar</button>
                                        </div>
                                    </div>
                                </div>
                            </li>
                        `;
                        cartList.append(listItem);
                    });
                } else {
                    $('#empty-cart-message').show();
                }
            },
            error: function (xhr, status, error) {
                console.error('Error al cargar el carrito:', error);
            }
        });
    }

    // Evento para cargar el carrito cuando se abra el offcanvas
    $('#offcanvasRight').on('show.bs.offcanvas', function () {
        fetchCartItems();
    });

    // Manejar la eliminación de productos del carrito
    $(document).on('click', '.eliminar-producto', function () {
        var idProducto = $(this).data('id');
        if (confirm('¿Estás seguro de que deseas eliminar este producto del carrito?')) {
            $.ajax({
                url: '/UsuarioFinal/eliminarProductoCarritoAjax/' + idProducto,
                type: 'DELETE',
                success: function (response) {
                    if (response.success) {
                        fetchCartItems(); // Actualizar la lista del carrito
                        // Actualizar contadores del carrito
                        $('#carrito-count-desktop').text(response.totalArticulos);
                        $('#carrito-count-mobile').text(response.totalArticulos);
                        animateCartIcon();
                    }
                },
                error: function (xhr, status, error) {
                    console.error('Error al eliminar el producto:', error);
                }
            });
        }
    });

    // Manejar la actualización de la cantidad de productos en el carrito
    $(document).on('click', '.actualizar-cantidad', function () {
        var button = $(this);
        var idProducto = button.data('id');
        var cantidadInput = button.closest('.d-flex').find('input[name="nuevaCantidad"]');
        var cantidad = parseInt(cantidadInput.val());
        var maxStock = parseInt(cantidadInput.attr('max')) || 9999;

        // Validar que la cantidad sea un número válido
        if (cantidad > maxStock) {
            alert('La cantidad seleccionada excede el stock disponible.');
            return;
        }

        if (isNaN(cantidad) || cantidad < 1) {
            alert('Cantidad inválida.');
            return;
        }

        $.ajax({
            url: '/UsuarioFinal/actualizarCantidadCarritoAjax',
            type: 'POST',
            data: {
                idProducto: idProducto,
                nuevaCantidad: cantidad
            },
            dataType: 'json',
            success: function (response) {
                if (response.success) {
                    fetchCartItems(); // Actualizar la lista del carrito
                    // Actualizar contadores del carrito
                    $('#carrito-count-desktop').text(response.totalArticulos);
                    $('#carrito-count-mobile').text(response.totalArticulos);
                    animateCartIcon();
                }
            },
            error: function (xhr, status, error) {
                console.error('Error al actualizar la cantidad:', error);
            }
        });
    });
});
