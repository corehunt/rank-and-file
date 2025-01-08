"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { HelpCircle } from "lucide-react";

const POLL_DATA = {
    question: "Which issue should Congress prioritize in 2024?",
    options: [
        { id: 1, text: "Economic Growth & Jobs", votes: 342 },
        { id: 2, text: "Healthcare Reform", votes: 285 },
        { id: 3, text: "Climate Change", votes: 264 },
        { id: 4, text: "National Security", votes: 198 }
    ]
};

export function PollQuestion() {
    const [voted, setVoted] = useState(false);
    const [selectedOption, setSelectedOption] = useState<number | null>(null);
    const totalVotes = POLL_DATA.options.reduce((sum, option) => sum + option.votes, 0);

    const handleVote = (optionId: number) => {
        if (!voted) {
            setSelectedOption(optionId);
            setVoted(true);
        }
    };

    return (
        <Card>
            <CardHeader className="flex flex-row items-center gap-2">
                <HelpCircle className="h-5 w-5" />
                <h2 className="text-xl font-semibold">Poll of the Day</h2>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <p className="font-medium">{POLL_DATA.question}</p>
                    <div className="space-y-3">
                        {POLL_DATA.options.map((option) => {
                            const voteCount = voted ? option.votes + (selectedOption === option.id ? 1 : 0) : option.votes;
                            const percentage = ((voteCount / (totalVotes + (voted ? 1 : 0))) * 100).toFixed(1);

                            return (
                                <div key={option.id}>
                                    <Button
                                        variant={selectedOption === option.id ? "default" : "outline"}
                                        className="w-full justify-between mb-2"
                                        onClick={() => handleVote(option.id)}
                                        disabled={voted && selectedOption !== option.id}
                                    >
                                        <span>{option.text}</span>
                                        {voted && <span>{percentage}%</span>}
                                    </Button>
                                    {voted && (
                                        <div className="h-2 rounded-full bg-muted overflow-hidden">
                                            <div
                                                className="h-full bg-primary transition-all duration-500"
                                                style={{ width: `${percentage}%` }}
                                            />
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                    <p className="text-sm text-muted-foreground text-center">
                        {voted ? (
                            `${totalVotes + 1} total votes`
                        ) : (
                            "Click an option to vote"
                        )}
                    </p>
                </div>
            </CardContent>
        </Card>
    );
}