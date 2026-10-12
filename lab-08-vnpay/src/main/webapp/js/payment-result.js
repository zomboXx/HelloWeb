document.addEventListener('DOMContentLoaded', () => {
    const dialog = document.querySelector('[data-success-dialog]');
    if (dialog && typeof dialog.showModal === 'function') {
        dialog.showModal();
    }

    const poll = document.querySelector('[data-order-poll]');
    if (!poll) return;

    let attempts = 0;
    async function checkStatus() {
        attempts += 1;
        try {
            const response = await fetch(poll.dataset.statusUrl, {
                credentials: 'same-origin', cache: 'no-store'
            });
            if (response.status === 404) {
                poll.textContent = 'Phiên đặt hàng đã hết hạn. Hãy quay lại danh sách CD để thử lại.';
                return;
            }
            if (response.ok) {
                const order = await response.json();
                if (order.status === 'PAID' || order.status === 'FAILED') {
                    window.location.replace(poll.dataset.resultUrl);
                    return;
                }
            }
        } catch (_) {
            // A transient network error should not hide the manual status link.
        }
        if (attempts < 15) {
            window.setTimeout(checkStatus, 4000);
        } else {
            poll.textContent = 'Vẫn chưa có IPN hợp lệ sau một phút. Kiểm tra IPN URL đã khai báo với VNPay rồi bấm “Kiểm tra lại trạng thái”.';
        }
    }
    window.setTimeout(checkStatus, 4000);
});
