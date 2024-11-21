"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface PoliticianFiltersProps {
  filters: {
    chamber: string;
    parties: string[];
    status: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function PoliticianFilters({ filters, onFilterChange }: PoliticianFiltersProps) {
  return (
    <Card className="sticky top-4">
      <CardHeader>
        <h2 className="text-lg font-semibold">Filters</h2>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <h3 className="font-medium">Chamber</h3>
          <RadioGroup 
            value={filters.chamber} 
            onValueChange={(value) => onFilterChange("chamber", value)}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="all" id="all" />
              <Label htmlFor="all">All Chambers</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="house" id="house" />
              <Label htmlFor="house">House</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="senate" id="senate" />
              <Label htmlFor="senate">Senate</Label>
            </div>
          </RadioGroup>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Party</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="democratic" 
                checked={filters.parties.includes("Democratic")}
                onCheckedChange={(checked) => {
                  const newParties = checked 
                    ? [...filters.parties, "Democratic"]
                    : filters.parties.filter(p => p !== "Democratic");
                  onFilterChange("parties", newParties);
                }}
              />
              <Label htmlFor="democratic">Democratic</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="republican"
                checked={filters.parties.includes("Republican")}
                onCheckedChange={(checked) => {
                  const newParties = checked 
                    ? [...filters.parties, "Republican"]
                    : filters.parties.filter(p => p !== "Republican");
                  onFilterChange("parties", newParties);
                }}
              />
              <Label htmlFor="republican">Republican</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="independent"
                checked={filters.parties.includes("Independent")}
                onCheckedChange={(checked) => {
                  const newParties = checked 
                    ? [...filters.parties, "Independent"]
                    : filters.parties.filter(p => p !== "Independent");
                  onFilterChange("parties", newParties);
                }}
              />
              <Label htmlFor="independent">Independent</Label>
            </div>
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Status</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="current"
                checked={filters.status.includes("current")}
                onCheckedChange={(checked) => {
                  const newStatus = checked 
                    ? [...filters.status, "current"]
                    : filters.status.filter(s => s !== "current");
                  onFilterChange("status", newStatus);
                }}
              />
              <Label htmlFor="current">Currently Serving</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="previous"
                checked={filters.status.includes("previous")}
                onCheckedChange={(checked) => {
                  const newStatus = checked 
                    ? [...filters.status, "previous"]
                    : filters.status.filter(s => s !== "previous");
                  onFilterChange("status", newStatus);
                }}
              />
              <Label htmlFor="previous">Previous Terms</Label>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}