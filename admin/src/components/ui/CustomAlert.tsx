import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle, CheckCircleIcon } from "lucide-react";

type VariantType = "default" | "destructive" | "success";

export interface CustomAlertProps {
    show?: boolean;
    variant: VariantType;
    onClose?: () => void;
    message: string;
    title?: string;
    iconAlert?: React.ReactNode;
}

export const CustomAlert = ({ variant, onClose, message, title, iconAlert, show }: CustomAlertProps) => {
    return (
        show && <Alert variant={variant} onClose={onClose}>
            {iconAlert ? iconAlert : variant === "success" ? <CheckCircleIcon className="h-4 w-4" /> : <AlertCircle className="h-4 w-4" />} 
            <AlertTitle>{title ? title : variant === "success" ? "Success" : "Error"}</AlertTitle>
            <AlertDescription>
                {message}
            </AlertDescription>
        </Alert>
    )
}