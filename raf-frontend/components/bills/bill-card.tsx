import { Card, CardHeader, CardContent } from "@/components/ui/card";
import Link from "next/link";

interface BillCardProps {
    billId: string;
    displayTitle: string;
    sponsorName?: string;
    sponsorParty?: string;
    sponsorState?: string;
    introducedDate?: string;
    summary?: string;
}

export default function BillCard({
                                     billId,
                                     displayTitle,
                                     sponsorName = "Unknown Sponsor",
                                     sponsorParty = "Unknown Party",
                                     sponsorState = "??",
                                     introducedDate,
                                     summary,
                                 }: BillCardProps) {
    const formatDate = (dateString?: string) => {
        if (!dateString) return "N/A";
        return new Date(dateString).toLocaleDateString("en-US", {
            year: "numeric",
            month: "long",
            day: "numeric",
        });
    };

    return (
        <Link href={`/bills/${billId}`}>
            <Card className="hover:border-primary/50 transition-colors cursor-pointer">
                <CardHeader>
                    <div className="space-y-2">
                        {/* Title */}
                        <h3 className="text-lg font-semibold">{displayTitle}</h3>

                        {/* Sponsor + Introduced */}
                        <p className="text-sm text-muted-foreground">
                            Sponsored by {sponsorName} ({sponsorParty}-{sponsorState})
                            {" • "}Introduced {formatDate(introducedDate)}
                        </p>
                    </div>
                </CardHeader>
                <CardContent>
                    <p className="text-muted-foreground line-clamp-2">
                        {summary || "No summary available."}
                    </p>
                </CardContent>
            </Card>
        </Link>
    );
}