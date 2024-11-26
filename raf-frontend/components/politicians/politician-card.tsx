import Image from "next/image";
import Link from "next/link";
import { Card, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface PoliticianCardProps {
    personId : string;
    name: string;
    state: string;
    party: string;
    district?: string;
    imageUrl: string;
    status: string;
    chamber: string;
}

export default function PoliticianCard({
                                           personId,
                                           name,
                                           state,
                                           party,
                                           district,
                                           imageUrl,
                                           status,
                                           chamber,
                                       }: PoliticianCardProps) {
    return (
        <Link href={`/politicians/${personId}`}>
            <Card className="hover:border-primary/50 transition-colors cursor-pointer h-full">
                <CardHeader className="space-y-0 pb-4">
                    <div className="flex flex-col sm:flex-row items-center gap-4">
                        <div className="relative h-32 w-32 rounded-lg overflow-hidden flex-shrink-0">
                            <Image
                                src={imageUrl}
                                alt={name}
                                fill
                                className="object-cover"
                            />
                        </div>
                        <div className="text-center sm:text-left space-y-2 flex-1">
                            <h3 className="text-lg font-semibold">{name}</h3>
                            <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
                                <Badge
                                    variant={
                                        party === "Democratic" ? "default" : "destructive"
                                    }
                                >
                                    {party}
                                </Badge>
                                <Badge variant="outline">{status}</Badge>
                            </div>
                            <div className="space-y-1 text-sm text-muted-foreground">
                                {/* Display state and district appropriately */}
                                {district && district !== "" ? (
                                    <p>
                                        {district}, {state}
                                    </p>
                                ) : (
                                    <p>{state}</p>
                                )}
                                <p className="capitalize">{chamber}</p>
                            </div>
                        </div>
                    </div>
                </CardHeader>
            </Card>
        </Link>
    );
}