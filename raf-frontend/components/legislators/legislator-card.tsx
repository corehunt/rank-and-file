import Image from "next/image";
import Link from "next/link";
import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface LegislatorCardProps {
  name: string;
  state: string;
  party: string;
  district: string;
  imageUrl: string;
}

export default function LegislatorCard({
  name,
  state,
  party,
  district,
  imageUrl,
}: LegislatorCardProps) {
  return (
    <Link href={`/legislators/${name.toLowerCase().replace(/\s+/g, "-")}`}>
      <Card className="hover:border-primary/50 transition-colors cursor-pointer">
        <CardHeader className="space-y-0 pb-4">
          <div className="flex items-center space-x-4">
            <div className="relative h-16 w-16 rounded-full overflow-hidden">
              <Image
                src={imageUrl}
                alt={name}
                fill
                className="object-cover"
              />
            </div>
            <div>
              <h3 className="text-lg font-semibold">{name}</h3>
              <p className="text-sm text-muted-foreground">{district}, {state}</p>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Badge
            variant={party === "Democratic" ? "default" : "destructive"}
            className="mt-2"
          >
            {party}
          </Badge>
        </CardContent>
      </Card>
    </Link>
  );
}