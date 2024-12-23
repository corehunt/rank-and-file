import Link from "next/link";
import Image from "next/image";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface PersonSummaryDTO {
    personId: string;
    fullName: string | null;
    partyMembership: string | null;
    currentDistrict: number | null;
    state: string | null;
    imageUrl: string | null;
}

interface LeadershipCardProps {
    leadershipId: string;
    leadershipType: string;
    currentLeader: string | boolean;
    person: PersonSummaryDTO;
}

export function LeadershipCard({
                                   leadershipId,
                                   leadershipType,
                                   currentLeader,
                                   person,
                               }: LeadershipCardProps) {
    // Convert membership code to text
    let partyText = "Independent";
    if (person.partyMembership === "R") partyText = "Republican";
    if (person.partyMembership === "D") partyText = "Democratic";

    // House vs. Senate district
    const district =
        person.currentDistrict !== null ? `District ${person.currentDistrict}` : null;

    return (
        <Link href={`/politicians/${person.personId}`}>
            <Card className="hover:border-primary/50 transition-colors h-full">
                <CardContent className="pt-6">
                    <div className="flex flex-col sm:flex-row items-center gap-4">
                        <div className="relative h-24 w-24 rounded-lg overflow-hidden flex-shrink-0">
                            <Image
                                src={person.imageUrl || "/default-image.jpg"}
                                alt={person.fullName ?? "Unknown"}
                                fill
                                className="object-cover"
                            />
                        </div>
                        {/* Details */}
                        <div className="text-center sm:text-left space-y-2">
                            <h3 className="font-semibold">{person.fullName ?? "Unknown"}</h3>
                            <p className="text-sm text-primary font-medium">
                                {leadershipType}
                            </p>
                            <div className="flex flex-wrap gap-2 justify-center sm:justify-start">
                                <Badge
                                    variant={partyText === "Democratic" ? "default" : "destructive"}
                                >
                                    {partyText}
                                </Badge>
                                {person.state && <Badge variant="outline">{person.state}</Badge>}
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </Link>
    );
}