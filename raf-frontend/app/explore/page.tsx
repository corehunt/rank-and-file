import { NewsFeed } from "./components/news-feed";
import { CongressControl } from "./components/congress-control";
import { PollQuestion } from "./components/poll-question";
import { KeyVotes } from "./components/key-votes";
import { TrendingTopics } from "./components/trending-topics";
import { Button } from "@/components/ui/button";
import { Users, FileText, ArrowRight } from "lucide-react";
import Link from "next/link";

export default function ExplorePage() {
  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <h1 className="text-2xl sm:text-3xl font-bold">Political Dashboard</h1>
            <p className="text-base sm:text-lg text-muted-foreground mt-2">
              Track congressional activity, key votes, and trending topics
            </p>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
          <CongressControl />

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Link href="/politicians" className="group">
              <div className="bg-card hover:bg-accent p-6 rounded-lg border hover:border-primary/50 transition-colors">
                <Users className="h-8 w-8 mb-4 text-primary" />
                <h2 className="text-xl font-semibold mb-2 group-hover:text-primary transition-colors">Search Politicians</h2>
                <p className="text-muted-foreground mb-4">
                  Explore voting records, sponsored bills, and financial connections of current and past Congress members.
                </p>
                <div className="flex items-center text-primary">
                  Start Exploring <ArrowRight className="h-4 w-4 ml-2 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            </Link>

            <Link href="/bills" className="group">
              <div className="bg-card hover:bg-accent p-6 rounded-lg border hover:border-primary/50 transition-colors">
                <FileText className="h-8 w-8 mb-4 text-primary" />
                <h2 className="text-xl font-semibold mb-2 group-hover:text-primary transition-colors">Search Bills</h2>
                <p className="text-muted-foreground mb-4">
                  Track and analyze current and historical legislation, including votes, amendments, and committee activity.
                </p>
                <div className="flex items-center text-primary">
                  View Legislation <ArrowRight className="h-4 w-4 ml-2 group-hover:translate-x-1 transition-transform" />
                </div>
              </div>
            </Link>
          </div>

          {/*<div className="grid grid-cols-1 lg:grid-cols-3 gap-8">*/}
          {/*  <div className="lg:col-span-2 space-y-8">*/}
          {/*    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">*/}
          {/*      <KeyVotes />*/}
          {/*      <TrendingTopics />*/}
          {/*    </div>*/}
          {/*    <PollQuestion />*/}
          {/*  </div>*/}

          {/*  <div>*/}
          {/*    <NewsFeed />*/}
          {/*  </div>*/}
          {/*</div>*/}
        </div>
      </div>
  );
}