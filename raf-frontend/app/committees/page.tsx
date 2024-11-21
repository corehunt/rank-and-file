"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import CommitteeCard from "@/components/committees/committee-card";
import CommitteeFilters from "@/components/committees/committee-filters";
import { 
  Sheet, 
  SheetContent, 
  SheetTrigger,
  SheetHeader,
  SheetTitle 
} from "@/components/ui/sheet";

const SAMPLE_COMMITTEES = [
  {
    id: 1,
    name: "House Committee on Energy and Commerce",
    chamber: "House",
    chair: "John Smith",
    rankingMember: "Jane Doe",
    memberCount: 52,
    description: "Oldest standing legislative committee in the U.S. House of Representatives with broad jurisdiction over energy, healthcare, and commerce.",
    type: "standing",
    policyAreas: ["energy", "healthcare"]
  },
  {
    id: 2,
    name: "Senate Committee on Foreign Relations",
    chamber: "Senate",
    chair: "Sarah Johnson",
    rankingMember: "Michael Brown",
    memberCount: 22,
    description: "One of the most influential committees in Congress, dealing with foreign policy, diplomatic relations, and international agreements.",
    type: "standing",
    policyAreas: ["foreign", "security"]
  }
];

export default function CommitteesPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: "all",
    type: [] as string[],
    policyAreas: [] as string[]
  });
  const [filteredCommittees, setFilteredCommittees] = useState(SAMPLE_COMMITTEES);

  const handleFilterChange = (type: string, value: string | string[]) => {
    setFilters(prev => ({
      ...prev,
      [type]: value
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
  };

  useEffect(() => {
    let filtered = SAMPLE_COMMITTEES;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(committee =>
        committee.name.toLowerCase().includes(query) ||
        committee.chair.toLowerCase().includes(query) ||
        committee.description.toLowerCase().includes(query)
      );
    }

    if (filters.chamber !== "all") {
      filtered = filtered.filter(committee => 
        committee.chamber.toLowerCase() === filters.chamber.toLowerCase()
      );
    }

    if (filters.type.length > 0) {
      filtered = filtered.filter(committee =>
        filters.type.includes(committee.type)
      );
    }

    if (filters.policyAreas.length > 0) {
      filtered = filtered.filter(committee =>
        committee.policyAreas.some(area => filters.policyAreas.includes(area))
      );
    }

    setFilteredCommittees(filtered);
  }, [searchQuery, filters]);

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
          <h1 className="text-2xl sm:text-3xl font-bold mb-4">Congressional Committees</h1>
          <p className="text-base sm:text-lg text-muted-foreground mb-6">
            Explore congressional committees, their members, and legislative activities.
          </p>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search committees..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Button type="submit">Search</Button>
          </form>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="lg:hidden mb-4">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" className="w-full">
                Filters
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-[300px] sm:w-[400px]">
              <SheetHeader>
                <SheetTitle>Filter Committees</SheetTitle>
              </SheetHeader>
              <div className="mt-6">
                <CommitteeFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
                />
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          <aside className="hidden lg:block lg:col-span-1">
            <CommitteeFilters
              filters={filters}
              onFilterChange={handleFilterChange}
            />
          </aside>
          <main className="lg:col-span-3">
            {filteredCommittees.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                No committees found matching your criteria
              </div>
            ) : (
              <div className="grid gap-6">
                {filteredCommittees.map((committee) => (
                  <CommitteeCard
                    key={committee.id}
                    name={committee.name}
                    chamber={committee.chamber}
                    chair={committee.chair}
                    rankingMember={committee.rankingMember}
                    memberCount={committee.memberCount}
                    description={committee.description}
                  />
                ))}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}