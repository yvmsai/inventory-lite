const money = new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const integer = new Intl.NumberFormat();
const dateTime = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });

/** Money arrives as a decimal string ("19.90"); format it for display only. */
export const formatMoney = (value: string) => money.format(Number(value));
export const formatNumber = (value: number) => integer.format(value);
export const formatDateTime = (iso: string) => dateTime.format(new Date(iso));
