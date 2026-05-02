/**
 * Инициализация модальных окон
 */
window.initModals = function() {
    function openModal($modal) {
        $modal.show();
    }
    function closeModal($modal) {
        $modal.hide();
    }
    $('.modal .close').on('click', function() {
        closeModal($(this).closest('.modal'));
    });
    $(window).on('click', function(e) {
        if ($(e.target).hasClass('modal')) {
            closeModal($(e.target));
        }
    });
    return { openModal, closeModal };
};