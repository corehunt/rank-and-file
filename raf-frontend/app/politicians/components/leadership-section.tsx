import { LeadershipCard } from "./leadership-card";

interface Leader {
    id: number;
    name: string;
    role: string;
    state: string;
    party: string;
    district: string;
    imageUrl: string;
}

interface LeadershipSectionProps {
    title: string;
    leaders: Leader[];
}

export function LeadershipSection({ title, leaders }: LeadershipSectionProps) {
    return (
        <div className="space-y-4">
            <h2 className="text-xl font-semibold">{title}</h2>
            <div className="grid gap-4 sm:grid-cols-2">
                {leaders.map((leader) => (
                    <LeadershipCard key={leader.id} {...leader} />
                ))}
            </div>
        </div>
    );
}