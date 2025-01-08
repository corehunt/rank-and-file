"use client";

import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Newspaper } from "lucide-react";
import Link from "next/link";

const NEWS_ITEMS = [
    {
        id: 1,
        title: "Senate Passes Major Infrastructure Bill",
        source: "Political Times",
        timestamp: "2 hours ago",
        category: "Legislation",
        url: "#"
    },
    {
        id: 2,
        title: "New Climate Change Proposals Introduced",
        source: "Capitol Report",
        timestamp: "4 hours ago",
        category: "Environment",
        url: "#"
    },
    {
        id: 3,
        title: "Tech Regulation Bill Gains Bipartisan Support",
        source: "DC Daily",
        timestamp: "6 hours ago",
        category: "Technology",
        url: "#"
    }
];

export function NewsFeed() {
    return (
        <Card>
            <CardHeader className="flex flex-row items-center gap-2">
                <Newspaper className="h-5 w-5" />
                <h2 className="text-xl font-semibold">Latest Political News</h2>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {NEWS_ITEMS.map((item) => (
                        <Link key={item.id} href={item.url} className="block">
                            <div className="group space-y-2 hover:bg-muted/50 p-3 rounded-lg transition-colors">
                                <div className="flex items-center justify-between">
                                    <h3 className="font-medium group-hover:text-primary transition-colors">
                                        {item.title}
                                    </h3>
                                    <Badge variant="outline">{item.category}</Badge>
                                </div>
                                <div className="flex items-center justify-between text-sm text-muted-foreground">
                                    <span>{item.source}</span>
                                    <span>{item.timestamp}</span>
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}