import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

export default function LegislatorFilters() {
  return (
    <Card className="sticky top-4">
      <CardHeader>
        <h2 className="text-lg font-semibold">Filters</h2>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <h3 className="font-medium">Chamber</h3>
          <RadioGroup defaultValue="all">
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
              <Checkbox id="democratic" />
              <Label htmlFor="democratic">Democratic</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="republican" />
              <Label htmlFor="republican">Republican</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="independent" />
              <Label htmlFor="independent">Independent</Label>
            </div>
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Status</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox id="current" defaultChecked />
              <Label htmlFor="current">Currently Serving</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox id="previous" />
              <Label htmlFor="previous">Previous Terms</Label>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}