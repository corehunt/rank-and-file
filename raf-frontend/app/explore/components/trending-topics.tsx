"use client";

import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { TrendingUp } from "lucide-react";
import Link from "next/link";

const TRENDING_TOPICS = [
    {
        id: 1,
        topic: "Infrastructure",
        count: 156,
        change: "+12%",
        trending: "up"
    },
    {
        id: 2,
        topic: "Healthcare",
        count: 134,
        change: "+8%",
        trending: "up"
    },
    {
        id: 3,
        topic: "Technology",
        count: 98,
        change: "+5%",
        trending: "up"
    }
];

export function TrendingTopics() {
    return (
        <Card>
            <CardHeader className="flex flex-row items-center gap-2">
                <TrendingUp className="h-5 w-5" />
                <h2 className="text-xl font-semibold">Trending Topics</h2>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {TRENDING_TOPICS.map((topic) => (
                        <Link key={topic.id} href={`/search?topic=${topic.topic.toLowerCase()}`}>
                            <div className="group hover:bg-muted/50 p-3 rounded-lg transition-colors">
                                <div className="flex items-center justify-between">
                                    <h3 className="font-medium group-hover:text-primary transition-colors">
                                        {topic.topic}
                                    </h3>
                                    <span className="text-sm text-green-500">{topic.change}</span>
                                </div>
                                <p className="text-sm text-muted-foreground">
                                    {topic.count} related bills this session
                                </p>
                            </div>
                        </Link>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}