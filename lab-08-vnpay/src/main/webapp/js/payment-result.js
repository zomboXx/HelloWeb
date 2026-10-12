document.addEventListener('DOMContentLoaded', () => {
    const dialog = document.querySelector('[data-success-dialog]');
    if (dialog && typeof dialog.showModal === 'function') {
        dialog.showModal();
    }
});
