export const formatDate = (date: Date) => {
    return new Date(date).toLocaleDateString('USA', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};