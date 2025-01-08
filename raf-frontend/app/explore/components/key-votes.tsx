"use client";

import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Vote, ThumbsUp, ThumbsDown } from "lucide-react";
import Link from "next/link";

const KEY_VOTES = [
    {
        id: "vote-123",
        billNumber: "H.R. 1234",
        title: "Infrastructure Investment Act",
        date: "2024-01-20",
        result: {
            yea: 245,
            nay: 190,
            notVoting: 3
        },
        passed: true
    },
    {
        id: "vote-124",
        billNumber: "S. 789",
        title: "Climate Action Plan",
        date: "2024-01-19",
        result: {
            yea: 52,
            nay: 48,
            notVoting: 0
        },
        passed: true
    }
];

export function KeyVotes() {
    return (
        <Card>
            <CardHeader className="flex flex-row items-center gap-2">
                <Vote className="h-5 w-5" />
                <h2 className="text-xl font-semibold">Recent Key Votes</h2>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {KEY_VOTES.map((vote) => (
                        <Link key={vote.id} href={`/bills/${vote.billNumber.toLowerCase()}`}>
                            <div className="group hover:bg-muted/50 p-3 rounded-lg transition-colors">
                                <div className="flex items-center justify-between mb-2">
                                    <div>
                                        <h3 className="font-medium group-hover:text-primary transition-colors">
                                            {vote.billNumber}
                                        </h3>
                                        <p className="text-sm text-muted-foreground">{vote.title}</p>
                                    </div>
                                    {vote.passed ? (
                                        <ThumbsUp className="h-5 w-5 text-green-500" />
                                    ) : (
                                        <ThumbsDown className="h-5 w-5 text-red-500" />
                                    )}
                                </div>
                                <div className="space-y-2">
                                    <div className="h-2 rounded-full bg-muted overflow-hidden">
                                        <div className="flex h-full">
                                            <div
                                                className="bg-green-500"
                                                style={{ width: `${(vote.result.yea / (vote.result.yea + vote.result.nay + vote.result.notVoting)) * 100}%` }}
                                            />
                                            <div
                                                className="bg-red-500"
                                                style={{ width: `${(vote.result.nay / (vote.result.yea + vote.result.nay + vote.result.notVoting)) * 100}%` }}
                                            />
                                        </div>
                                    </div>
                                    <div className="flex justify-between text-sm text-muted-foreground">
                                        <span>Yea: {vote.result.yea}</span>
                                        <span>Nay: {vote.result.nay}</span>
                                        {vote.result.notVoting > 0 && (
                                            <span>Not Voting: {vote.result.notVoting}</span>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}