"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import PoliticianCard from "@/components/politicians/politician-card";
import PoliticianFilters from "@/components/politicians/politician-filters";
import { 
  Sheet, 
  SheetContent, 
  SheetTrigger,
  SheetHeader,
  SheetTitle 
} from "@/components/ui/sheet";

const SAMPLE_POLITICIANS = [
  {
    id: 1,
    name: "Jane Smith",
    state: "CA",
    party: "Democratic",
    district: "12th District",
    imageUrl: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200&h=200",
    chamber: "house",
    status: "current"
  },
  {
    id: 2,
    name: "John Doe",
    state: "TX",
    party: "Republican",
    district: "7th District",
    imageUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=200&h=200",
    chamber: "house",
    status: "current"
  },
  {
    id: 3,
    name: "Sarah Johnson",
    state: "NY",
    party: "Democratic",
    district: "Senate",
    imageUrl: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200&h=200",
    chamber: "senate",
    status: "current"
  },
  {
    id: 4,
    name: "Michael Brown",
    state: "FL",
    party: "Republican",
    district: "Senate",
    imageUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=200&h=200",
    chamber: "senate",
    status: "previous"
  }
];

export default function PoliticiansPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: "all",
    parties: [] as string[],
    status: ["current"] as string[]
  });
  const [filteredPoliticians, setFilteredPoliticians] = useState(SAMPLE_POLITICIANS);

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
    let filtered = SAMPLE_POLITICIANS;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(politician =>
        politician.name.toLowerCase().includes(query) ||
        politician.state.toLowerCase().includes(query) ||
        politician.district.toLowerCase().includes(query)
      );
    }

    if (filters.chamber !== "all") {
      filtered = filtered.filter(politician => 
        politician.chamber === filters.chamber
      );
    }

    if (filters.parties.length > 0) {
      filtered = filtered.filter(politician =>
        filters.parties.includes(politician.party)
      );
    }

    if (filters.status.length > 0) {
      filtered = filtered.filter(politician =>
        filters.status.includes(politician.status)
      );
    }

    setFilteredPoliticians(filtered);
  }, [searchQuery, filters]);

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
          <h1 className="text-2xl sm:text-3xl font-bold mb-4">U.S. Politicians</h1>
          <p className="text-base sm:text-lg text-muted-foreground mb-6">
            Track voting records, sponsored bills, and financial connections of current and past Congress members.
          </p>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search by name, state, or district..."
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
                <SheetTitle>Filter Politicians</SheetTitle>
              </SheetHeader>
              <div className="mt-6">
                <PoliticianFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
                />
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          <aside className="hidden lg:block lg:col-span-1">
            <PoliticianFilters
              filters={filters}
              onFilterChange={handleFilterChange}
            />
          </aside>
          <main className="lg:col-span-3">
            {filteredPoliticians.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                No politicians found matching your criteria
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredPoliticians.map((politician) => (
                  <PoliticianCard
                    key={politician.id}
                    name={politician.name}
                    state={politician.state}
                    party={politician.party}
                    district={politician.district}
                    imageUrl={politician.imageUrl}
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