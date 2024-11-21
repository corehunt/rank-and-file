import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";

interface TradeCardProps {
  politician: string;
  symbol: string;
  company: string;
  type: string;
  amount: string;
  date: string;
  disclosure: string;
}

export default function TradeCard({
  politician,
  symbol,
  company,
  type,
  amount,
  date,
  disclosure,
}: TradeCardProps) {
  return (
    <Card className="hover:border-primary/50 transition-colors">
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div>
            <Link href={`/politicians/${politician.toLowerCase().replace(/\s+/g, "-")}`} className="hover:underline">
              <h3 className="text-lg font-semibold">{politician}</h3>
            </Link>
            <p className="text-sm text-muted-foreground">{company} ({symbol})</p>
          </div>
          <Badge variant={type === "Purchase" ? "default" : "secondary"}>{type}</Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-3 gap-4">
          <div>
            <p className="text-sm font-medium">Amount</p>
            <p className="text-sm text-muted-foreground">{amount}</p>
          </div>
          <div>
            <p className="text-sm font-medium">Date</p>
            <p className="text-sm text-muted-foreground">
              {new Date(date).toLocaleDateString()}
            </p>
          </div>
          <div>
            <p className="text-sm font-medium">Disclosure</p>
            <p className="text-sm text-muted-foreground">{disclosure}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}