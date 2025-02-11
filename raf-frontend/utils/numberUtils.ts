export function getNumberSuffix(num: number | null | undefined){
    const lastDigit = num % 10;
    const lastTwoDigits = num % 100;

    // Handle special cases for numbers ending in 11, 12, 13
    if (lastTwoDigits >= 11 && lastTwoDigits <= 13) {
        return "th";
    }

    // General cases for other numbers
    switch (lastDigit) {
        case 1:
            return "st";
        case 2:
            return "nd";
        case 3:
            return "rd";
        default:
            return "th";
    }
}