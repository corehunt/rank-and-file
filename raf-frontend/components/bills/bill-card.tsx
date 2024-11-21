import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";

interface BillCardProps {
  title: string;
  sponsor: string;
  party: string;
  state: string;
  status: string;
  introducedDate: string;
  summary: string;
}

export default function BillCard({
  title,
  sponsor,
  party,
  state,
  status,
  introducedDate,
  summary,
}: BillCardProps) {
  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "passed house":
      case "passed senate":
        return "success";
      case "in committee":
        return "warning";
      case "failed":
        return "destructive";
      default:
        return "default";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <Link href={`/bills/${title.toLowerCase().replace(/[^a-z0-9]+/g, "-")}`}>
      <Card className="hover:border-primary/50 transition-colors cursor-pointer">
        <CardHeader>
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">{title}</h3>
              <Badge variant={getStatusColor(status)}>{status}</Badge>
            </div>
            <p className="text-sm text-muted-foreground">
              Sponsored by {sponsor} ({party}-{state}) • Introduced {formatDate(introducedDate)}
            </p>
          </div>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground line-clamp-2">{summary}</p>
        </CardContent>
      </Card>
    </Link>
  );
}