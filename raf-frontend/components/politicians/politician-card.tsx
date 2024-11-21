import Image from "next/image";
import Link from "next/link";
import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface PoliticianCardProps {
  name: string;
  state: string;
  party: string;
  district: string;
  imageUrl: string;
}

export default function PoliticianCard({
  name,
  state,
  party,
  district,
  imageUrl,
}: PoliticianCardProps) {
  return (
    <Link href={`/politicians/${name.toLowerCase().replace(/\s+/g, "-")}`}>
      <Card className="hover:border-primary/50 transition-colors cursor-pointer h-full">
        <CardHeader className="space-y-0 pb-4">
          <div className="flex flex-col sm:flex-row items-center gap-4">
            <div className="relative h-20 w-20 rounded-full overflow-hidden flex-shrink-0">
              <Image
                src={imageUrl}
                alt={name}
                fill
                className="object-cover"
              />
            </div>
            <div className="text-center sm:text-left">
              <h3 className="text-lg font-semibold">{name}</h3>
              <p className="text-sm text-muted-foreground">{district}, {state}</p>
            </div>
          </div>
        </CardHeader>
        <CardContent className="text-center sm:text-left">
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