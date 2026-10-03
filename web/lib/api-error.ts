export class ApiError extends Error {
    status: number;
    title?: string;
    instance?: string;

    constructor(message: string, status: number, extra?: { title?: string; instance?: string }) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.title = extra?.title;
        this.instance = extra?.instance;
        Object.setPrototypeOf(this, ApiError.prototype);
    }
}