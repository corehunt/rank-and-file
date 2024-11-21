import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";

interface CommitteeCardProps {
  name: string;
  chamber: string;
  chair: string;
  rankingMember: string;
  memberCount: number;
  description: string;
}

export default function CommitteeCard({
  name,
  chamber,
  chair,
  rankingMember,
  memberCount,
  description,
}: CommitteeCardProps) {
  return (
    <Link href={`/committees/${name.toLowerCase().replace(/\s+/g, "-")}`}>
      <Card className="hover:border-primary/50 transition-colors cursor-pointer">
        <CardHeader>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">{name}</h3>
              <Badge variant={chamber === "House" ? "default" : "secondary"}>{chamber}</Badge>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <p className="text-muted-foreground">{description}</p>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium">Chair</p>
                <p className="text-sm text-muted-foreground">{chair}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Ranking Member</p>
                <p className="text-sm text-muted-foreground">{rankingMember}</p>
              </div>
            </div>
            <div className="text-sm text-muted-foreground">
              {memberCount} Members
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}