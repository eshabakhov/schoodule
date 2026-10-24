const back = document.getElementById('error-back');
if (back) {
    back.addEventListener('click', event => {
        if (window.history.length > 1) {
            event.preventDefault();
            window.history.back();
        }
    });
}
