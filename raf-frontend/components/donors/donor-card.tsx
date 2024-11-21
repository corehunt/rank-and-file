import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";

interface Recipient {
  name: string;
  amount: string;
}

interface DonorCardProps {
  name: string;
  type: string;
  totalDonations: string;
  topRecipients: Recipient[];
  industries: string[];
}

export default function DonorCard({
  name,
  type,
  totalDonations,
  topRecipients,
  industries,
}: DonorCardProps) {
  return (
    <Link href={`/donors/${name.toLowerCase().replace(/\s+/g, "-")}`}>
      <Card className="hover:border-primary/50 transition-colors cursor-pointer">
        <CardHeader>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">{name}</h3>
              <Badge variant="outline">{type}</Badge>
            </div>
            <p className="text-2xl font-bold text-primary">{totalDonations}</p>
            <p className="text-sm text-muted-foreground">Total Donations (2023-2024)</p>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div>
              <h4 className="text-sm font-medium mb-2">Top Recipients</h4>
              <ul className="space-y-1">
                {topRecipients.map((recipient, index) => (
                  <li key={index} className="text-sm text-muted-foreground flex justify-between">
                    <span>{recipient.name}</span>
                    <span>{recipient.amount}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <h4 className="text-sm font-medium mb-2">Industries</h4>
              <div className="flex flex-wrap gap-2">
                {industries.map((industry, index) => (
                  <Badge key={index} variant="secondary">{industry}</Badge>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}