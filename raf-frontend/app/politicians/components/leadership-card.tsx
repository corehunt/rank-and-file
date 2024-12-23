import Link from "next/link";
import Image from "next/image";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface LeadershipCardProps {
    name: string;
    role: string;
    state: string;
    party: string;
    district: string;
    imageUrl: string;
}

export function LeadershipCard({
                                   name,
                                   role,
                                   state,
                                   party,
                                   district,
                                   imageUrl
                               }: LeadershipCardProps) {
    return (
        <Link href={`/politicians/${name.toLowerCase().replace(/\s+/g, "-")}`}>
            <Card className="hover:border-primary/50 transition-colors h-full">
                <CardContent className="pt-6">
                    <div className="flex flex-col sm:flex-row items-center gap-4">
                        <div className="relative h-24 w-24 rounded-lg overflow-hidden flex-shrink-0">
                            <Image
                                src={imageUrl}
                                alt={name}
                                fill
                                className="object-cover"
                            />
                        </div>
                        <div className="text-center sm:text-left space-y-2">
                            <h3 className="font-semibold">{name}</h3>
                            <p className="text-sm text-primary font-medium">{role}</p>
                            <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
                                <Badge variant={party === "Democratic" ? "default" : "destructive"}>
                                    {party}
                                </Badge>
                                <Badge variant="outline">{state}</Badge>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </Link>
    );
}